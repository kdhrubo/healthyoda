let mediaRecorder = null;
let audioChunks = [];

// UI Elements
const recordInitial = document.getElementById('recordInitial');
const recordActive = document.getElementById('recordActive');
const recordReview = document.getElementById('recordReview');
const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');
const rerecordButton = document.getElementById('rerecordButton');
const audioPlayer = document.getElementById('audioPlayer');
const audioFile = document.getElementById('audioFile');

function showState(state) {
    recordInitial.style.display = state === 'initial' ? 'block' : 'none';
    recordActive.style.display = state === 'recording' ? 'block' : 'none';
    recordReview.style.display = state === 'review' ? 'block' : 'none';
}

startButton.addEventListener('click', async () => {
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
            
            // Setup audio player
            audioPlayer.src = audioUrl;

            // Attach file to form
            const audioFile = new File([audioBlob], 'recording.webm', { type: 'audio/webm' });
            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(audioFile);
            document.getElementById('audioFile').files = dataTransfer.files;
            
            // Show review state
            showState('review');
            
            // Stop and release media stream
            stream.getTracks().forEach(track => track.stop());
        };

        mediaRecorder.start();
        showState('recording');

    } catch (err) {
        console.error('Error accessing microphone:', err);
        alert('Error accessing microphone. Please ensure you have given permission.');
    }
});

stopButton.addEventListener('click', () => {
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
    }
});

rerecordButton.addEventListener('click', () => {
    showState('initial');
});
