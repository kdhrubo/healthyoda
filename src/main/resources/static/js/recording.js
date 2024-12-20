let mediaRecorder = null;
let audioChunks = [];

document.getElementById('startButton').addEventListener('click', async () => {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        mediaRecorder = new MediaRecorder(stream);
        audioChunks = [];

        mediaRecorder.ondataavailable = (event) => {
            audioChunks.push(event.data);
        };

        mediaRecorder.onstop = () => {
            const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
            const audioUrl = URL.createObjectURL(audioBlob);
            
            // Show audio player
            const audio = document.getElementById('audioPlayer');
            audio.src = audioUrl;
            audio.style.display = 'block';

            // Attach file to form
            const audioFile = new File([audioBlob], 'recording.webm', { type: 'audio/webm' });
            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(audioFile);
            document.getElementById('audioFile').files = dataTransfer.files;
            
            // Show upload form
            document.querySelector('form').style.display = 'block';
            
            // Stop and release media stream
            stream.getTracks().forEach(track => track.stop());
        };

        mediaRecorder.start();
        document.getElementById('startButton').disabled = true;
        document.getElementById('stopButton').disabled = false;
        document.querySelector('.recording').style.display = 'inline';
        document.querySelector('form').style.display = 'none';

    } catch (err) {
        console.error('Error accessing microphone:', err);
        alert('Error accessing microphone. Please ensure you have given permission.');
    }
});

document.getElementById('stopButton').addEventListener('click', () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
        document.getElementById('startButton').disabled = false;
        document.getElementById('stopButton').disabled = true;
        document.querySelector('.recording').style.display = 'none';
    }
});
